package by.zharski.idftechtask.controller;

import by.zharski.idftechtask.dto.ExpenseLimitDto;
import by.zharski.idftechtask.dto.TransactionDto;
import by.zharski.idftechtask.service.ExpenseLimitService;
import by.zharski.idftechtask.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final TransactionService transactionService;
    private final ExpenseLimitService expenseLimitService;

    public ClientController(TransactionService transactionService, ExpenseLimitService expenseLimitService) {
        this.transactionService = transactionService;
        this.expenseLimitService = expenseLimitService;
    }

    @GetMapping("/exceededLimitTransactions")
    public List<TransactionDto> getTransactionsExceededLimit(@RequestParam Long accountId) {
        return transactionService.getLimitExceedingTransactions(accountId);
    }

    @PostMapping("/limit")
    public ExpenseLimitDto setNewLimit(@RequestBody ExpenseLimitDto expenseLimitDto) {
        return expenseLimitService.createExpenseLimit(expenseLimitDto);
    }

    @GetMapping("/limits")
    public List<ExpenseLimitDto> getAllLimits(@RequestParam Long accountId) {
        return expenseLimitService.getAllExpenseLimits(accountId);
    }

}
