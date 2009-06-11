package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.Arrays;
import java.util.Comparator;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

public class SortUsers {

    public static void sort(String[] list) {
        Arrays.sort(list, new Comparator<String>() {
            public int compare(String s1, String s2) {
		if (s1.startsWith(NameTagNode.LEFT_MUTE)) {
		    s1 = s1.substring(1);
		}

		if (s2.startsWith(NameTagNode.LEFT_MUTE)) {
		    s2 = s2.substring(1);
		}
                
		return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            }
        });
    }

}
