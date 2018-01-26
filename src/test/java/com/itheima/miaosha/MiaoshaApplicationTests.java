package com.itheima.miaosha;

import com.itheima.miaosha.activemq.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MiaoshaApplicationTests {

	@Autowired
	private Producer producer;

	@Test
	public void contextLoads() {
//		producer.send();
		System.out.println("\"信息发送了\" = " + "信息发送了");
	}

}
