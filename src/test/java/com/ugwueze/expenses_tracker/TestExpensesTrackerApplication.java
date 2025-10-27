package com.ugwueze.expenses_tracker;

import org.springframework.boot.SpringApplication;

public class TestExpensesTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.from(ExpensesTrackerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
