package by.zharski.idftechtask;

import by.zharski.idftechtask.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdftechtaskApplicationTests {

	@Autowired
	TransactionRepository transactionRepository;

	@Test
	void contextLoads() {
	}

}
