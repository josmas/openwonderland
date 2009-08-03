
package org.jdesktop.wonderland.modules.avatarbase.client.imi;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;

/**
 * Demo code.
 * @author Ronald E Dahlgren
 */
public class ImiDemoAvatarFactory {
    // Female
    private static final List<WonderlandCharacterParams> femaleParams = new FastList();
    private static Iterator<WonderlandCharacterParams> femaleParamIter = femaleParams.iterator();
    // Male
    private static final List<WonderlandCharacterParams> maleParams = new FastList();
    private static Iterator<WonderlandCharacterParams> maleParamIter = maleParams.iterator();
    
    public static void init()
    {
        try {
            // initialize the prototypical WL params
            WonderlandCharacterParams params = null;
            ////////////// Females //////////////
            // Config 1
            params = WonderlandCharacterParams.loadFemale(); // Get defaults, init metadata
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HAIR, "Med_Pt_BangzShape");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HEAD, "Female Low-Poly Head");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.LEGS, "Dress Pants");
            femaleParams.add(params);
            // Config 2
            params = WonderlandCharacterParams.loadFemale(); // Get defaults, init metadata
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HAIR, "Med_Pt_BangzShape");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HEAD, "Female Low-Poly Head");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.LEGS, "Dress Pants");
            femaleParams.add(params);
            // Config 3
            params = WonderlandCharacterParams.loadFemale(); // Get defaults, init metadata
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HAIR, "Med_Pt_BangzShape");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HEAD, "Female Low-Poly Head");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.LEGS, "Dress Pants");
            femaleParams.add(params);
            // Config 4
            params = WonderlandCharacterParams.loadFemale(); // Get defaults, init metadata
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HAIR, "Med_Pt_BangzShape");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.HEAD, "Female Low-Poly Head");
            params.setElementPreset(WonderlandCharacterParams.ConfigType.LEGS, "Dress Pants");
            femaleParams.add(params);

            //////////////// Males //////////////
            // Config 1
            params = WonderlandCharacterParams.loadMale(); // Get defaults, init metadata
            // Config 2
            // Config 3
            // Config 4
            maleParams.add(params);
            // Done
            initialized = true;
        } catch (IOException ex) {
            // I can't really do much if this fails...
        }
    }
    
    private static boolean initialized = false;

    public static synchronized WonderlandCharacterParams getNextMaleParams() {
        if (!initialized)
            init();
        if (femaleParamIter.hasNext() == false)
            femaleParamIter = femaleParams.iterator();
        return femaleParamIter.next();


    }
    public static synchronized WonderlandCharacterParams getNextFemaleParams() {
        if (!initialized)
            init();
        if (femaleParamIter.hasNext() == false)
            femaleParamIter = femaleParams.iterator();
        return femaleParamIter.next();
    }
}
