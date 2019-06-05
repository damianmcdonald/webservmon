package com.github.damianmcdonald.webservmon.mailers;

import java.util.Map;

public interface Mailer<T1, T2> {

    void sendMail(final boolean hasErrors, final Map<T1, T2> results);

}
