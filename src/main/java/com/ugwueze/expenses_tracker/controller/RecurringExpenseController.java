package com.ugwueze.expenses_tracker.controller;

import com.ugwueze.expenses_tracker.entity.RecurringExpense;
import com.ugwueze.expenses_tracker.repository.RecurringExpenseRepository;
import com.ugwueze.expenses_tracker.service.RecurringExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses/recurring")
public class RecurringExpenseController {

    @Autowired
    private RecurringExpenseRepository repo;

    @Autowired
    private RecurringExpenseService service;

    public RecurringExpenseController(RecurringExpenseRepository repo,
                                      RecurringExpenseService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping
    public RecurringExpense create(@RequestBody RecurringExpense template) {
        return repo.save(template);
    }

    @GetMapping
    public List<RecurringExpense> list() {
        return repo.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/process")
    public void triggerProcess() {
        service.processDueRecurringExpenses();
    }
}
