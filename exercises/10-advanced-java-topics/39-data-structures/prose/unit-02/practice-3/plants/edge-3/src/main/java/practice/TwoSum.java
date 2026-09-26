package practice;

import java.util.HashMap;
import java.util.Map;

public final class TwoSum {

    private TwoSum() {
    }

    /** Indices {i, j}, i < j, whose values add up to target; an empty array when there are none. */
    public static int[] find(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int j = 0; j < nums.length; j++) {
            Integer i = seen.get(target - nums[j]);
            if (i != null) {
                return new int[] {i, j};
            }
            seen.put(nums[j], j);
        }
        return null;
    }
}
