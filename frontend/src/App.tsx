import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Customer = {
  id: number
  name: string
  email: string | null
  phone: string | null
  address: string | null
}

const customerFields = [
  { name: 'name', label: 'Name', type: 'text' },
  { name: 'email', label: 'Email (optional)', type: 'email' },
  { name: 'phone', label: 'Phone (optional)', type: 'tel' },
  { name: 'address', label: 'Address (optional)', type: 'text' },
] as const

function AddCustomer({ token, onCreated }: { token: string; onCreated: () => void }) {
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [notice, setNotice] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (saving) return

    const form = event.currentTarget
    const data = new FormData(form)
    const payload = Object.fromEntries(customerFields.map(({ name }) => {
      const value = String(data.get(name) ?? '').trim()
      return [name, name === 'name' ? value : value || null]
    }))

    setSaving(true)

    setErrors({})
    setNotice('')
    try {
      const response = await fetch('/api/customers', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      if (response.ok) {
        form.reset()
        setNotice('Customer added.')
        onCreated()
        return
      }

      const body: Record<string, unknown> | null = await response.json().catch(() => null)
      const fieldErrors: Record<string, string> = {}
      for (const { name } of customerFields) {
        const message = body?.[name]
        if (typeof message === 'string') fieldErrors[name] = message
      }
      if (Object.keys(fieldErrors).length > 0) {
        setErrors(fieldErrors)
      } else {
        const message = response.status === 401
          ? 'Session expired. Sign out and sign in again.'
          : response.status === 403
            ? 'Your account cannot add customers.'
            : typeof body?.error === 'string'
              ? body.error
              : `Could not add customer (HTTP ${response.status}).`
        setErrors({ general: message })
      }
    } catch {
      setErrors({ general: 'Could not reach the server. Try again.' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="add-customer" aria-labelledby="add-customer-heading">
      <h2 id="add-customer-heading">Add customer</h2>
      <form onSubmit={handleSubmit} noValidate>
        <fieldset disabled={saving} className="customer-fields">
          {customerFields.map(({ name, label, type }) => (
            <label key={name}>
              {label}
              <input
                name={name}
                type={type}
                required={name === 'name'}
                aria-invalid={Boolean(errors[name])}
                aria-describedby={errors[name] ? `customer-${name}-error` : undefined}
              />
              {errors[name] && <span id={`customer-${name}-error`} role="alert">{errors[name]}</span>}
            </label>
          ))}
        </fieldset>
        {errors.general && <p role="alert">{errors.general}</p>}
        <button type="submit" disabled={saving}>{saving ? 'Saving…' : 'Add customer'}</button>
        {notice && <p role="status">{notice}</p>}
      </form>
    </section>
  )
}

function CustomerList({ token, signOut }: { token: string; signOut: () => void }) {
  const [customers, setCustomers] = useState<Customer[] | null>(null)
  const [error, setError] = useState('')
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      try {
        const response = await fetch('/api/customers', {
          headers: { Authorization: `Bearer ${token}` },
          signal: controller.signal,
        })

        if (response.status === 401) throw new Error('Session expired. Sign out and sign in again.')
        if (response.status === 403) throw new Error('Only managers and cashiers can list customers.')
        if (!response.ok) throw new Error(`Could not load customers (HTTP ${response.status}).`)

        const data: Customer[] = await response.json()
        if (!Array.isArray(data)) throw new Error('The server returned an unexpected customer response.')
        if (!controller.signal.aborted) setCustomers(data)
      } catch (error) {
        if (!controller.signal.aborted) {
          setError(error instanceof Error ? error.message : 'Could not load customers.')
        }
      }
    }

    void load()
    return () => controller.abort()
  }, [token, revision])

  return (
    <main className="panel dashboard">
      <header className="page-header">
        <h1>Customers</h1>
        <button className="secondary" onClick={signOut}>Sign out</button>
      </header>
      <AddCustomer token={token} onCreated={() => {
        setError('')
        setRevision((previous) => previous + 1)
      }} />
      {error ? <p role="alert">{error}</p> : customers === null ? (
        <p role="status">Loading customers…</p>
      ) : customers.length === 0 ? (
        <p>No customers found.</p>
      ) : (
        <div className="table-scroll" role="region" aria-label="Customer list" tabIndex={0}>
          <table aria-label="Customers">
            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">Email</th>
                <th scope="col">Phone</th>
                <th scope="col">Address</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.name}</td>
                  <td>{customer.email ?? '—'}</td>
                  <td>{customer.phone ?? '—'}</td>
                  <td>{customer.address ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  )
}

export default function App() {
  const [token, setToken] = useState('')
  const [loginError, setLoginError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting) return

    const form = new FormData(event.currentTarget)
    setLoginError('')
    setSubmitting(true)
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: String(form.get('email') ?? '').trim(),
          password: form.get('password'),
        }),
      })
      if (response.status === 401 || response.status === 403) {
        throw new Error('Sign-in failed. Check your email and password.')
      }
      if (!response.ok) throw new Error(`Sign-in failed (HTTP ${response.status}).`)

      const data: { token?: unknown } = await response.json()
      if (typeof data.token !== 'string' || !data.token) {
        throw new Error('The server did not return a sign-in token.')
      }
      setToken(data.token)
    } catch (error) {
      setLoginError(error instanceof Error ? error.message : 'Sign-in failed.')
    } finally {
      setSubmitting(false)
    }
  }

  if (token) return <CustomerList token={token} signOut={() => setToken('')} />

  return (
    <main className="panel sign-in">
      <h1>Sign in</h1>
      <p>Use your account to view the customer directory.</p>
      <form onSubmit={handleLogin}>
        <label>Email
          <input name="email" type="email" autoComplete="username" required />
        </label>
        <label>Password
          <input name="password" type="password" autoComplete="current-password" required />
        </label>
        {loginError && <p role="alert">{loginError}</p>}
        <button type="submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</button>
      </form>
    </main>
  )
}
