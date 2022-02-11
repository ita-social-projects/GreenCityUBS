package greencity.mapping;

import greencity.ModelUtils;
import greencity.entity.user.employee.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PositionMapperTest {
    @InjectMocks
    PositionMapper positionMapper;

    @Test
    void convert() {
        Position expected = ModelUtils.getPosition();
        Position actual = positionMapper.convert(ModelUtils.getPositionDto());

        assertEquals(expected, actual);
    }
}
