package com.apps.unisabanetaapp;

import android.net.LocalSocketAddress;
import android.support.v4.app.Fragment;

import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

/**
 * Created by checho on 23/08/17.
 */

public class SoapAutenticationBuild extends Fragment {
    protected static Element buildAuthHeader(String NAMESPACE, String USER, String PASS) {
        Element h = new Element().createElement(NAMESPACE, "AuthSoapHd");
        Element username = new Element().createElement(NAMESPACE, "strUserName");
        username.addChild(Node.TEXT, USER);
        h.addChild(Node.ELEMENT, username);
        Element pass = new Element().createElement(NAMESPACE, "strPassword");
        pass.addChild(Node.TEXT, PASS);
        h.addChild(Node.ELEMENT, pass);
        return h;
    }
}
