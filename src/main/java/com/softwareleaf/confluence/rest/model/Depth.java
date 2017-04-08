package com.softwareleaf.confluence.rest.model;

/**
 * @author Ján Pichanič
 */
public enum Depth {

    ALL, ROOT;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}
