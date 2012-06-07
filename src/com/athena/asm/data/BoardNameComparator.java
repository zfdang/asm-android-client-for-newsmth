package com.athena.asm.data;

import java.util.Comparator;

public class BoardNameComparator implements Comparator<Board>{

	@Override
	public int compare(Board lhs, Board rhs) {
		String lName = lhs.getEngName().toLowerCase();
		String rName = rhs.getEngName().toLowerCase();
		if (lName == null && rName != null) {
			return -1;
		} else if (lName != null && rName == null) {
			return 1;
		} else if (lName == null && rName == null) {
			return 0;
		} else {
			return lName.compareTo(rName); 
		}
	}

}
