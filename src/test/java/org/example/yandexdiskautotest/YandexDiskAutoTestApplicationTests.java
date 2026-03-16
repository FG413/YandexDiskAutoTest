package org.example.yandexdiskautotest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YandexDiskAutoTestApplicationTests
{

    @Order(1)
    @Test
    void getTrash()
    {
        var x = 1;
        Assertions.assertEquals(1,2);
    }

}
