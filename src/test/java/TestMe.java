import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.assertTrue;

public class TestMe {

    @Test
    public void test(){
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();

        long diff = startDate.until(now, ChronoUnit.MILLIS);

        assertTrue(diff < 60000);
    }
}
