package com.softwareleaf.confluence.rest.util;

import com.softwareleaf.confluence.rest.model.Expandable;

/**
 */
public class Expand {

    private StringBuilder sb = new StringBuilder();

    public Expand expand(Expandable expand) {
        if (sb.length() != 0) {
            sb.append(",");
        }
        sb.append(expand);
        return this;
    }

    public Expand nestedExpand(Expandable... nestedExpand) {
        if (sb.length() != 0) {
            sb.append(",");
        }
        for (int i = 0; i < nestedExpand.length; i++) {
            sb.append(nestedExpand[i]);
            if (i != nestedExpand.length - 1) {
                sb.append(".");
            }
        }
        return this;
    }

    public String toQueryParams() {
        return sb.toString();
    }

}
