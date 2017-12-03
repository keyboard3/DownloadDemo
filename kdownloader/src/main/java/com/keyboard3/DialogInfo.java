package com.keyboard3;

import java.io.Serializable;

/**
 * @author keyboard3 on 2017/12/3
 */

public class DialogInfo implements Serializable {
    public String title;
    public String message;
    public String positiveText;
    public String negativeText;
    public boolean forceShow;
}
