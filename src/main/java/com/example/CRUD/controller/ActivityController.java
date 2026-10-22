package com.example.CRUD.controller;

import com.example.CRUD.service.ActivityFeed;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityFeed feed;

    public ActivityController(ActivityFeed feed) {
        this.feed = feed;
    }

    @GetMapping
    public List<String> recent(@RequestParam(defaultValue = "20") int limit) {
        return feed.recent(Math.max(1, limit));
    }
}