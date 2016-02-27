package examination.domain.solver;

import examination.domain.RoomPeriod;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * Created by Gustavo on 27/02/2016.
 */
public class RoomPeriodDifficultyComparator implements Comparator<RoomPeriod> {
    @Override
    public int compare(RoomPeriod rp1, RoomPeriod rp2) {
        return new CompareToBuilder()
                .append(rp1.getPeriod().getPeriodTime().ordinal(), rp2.getPeriod().getPeriodTime().ordinal())
                .toComparison();
    }
}
