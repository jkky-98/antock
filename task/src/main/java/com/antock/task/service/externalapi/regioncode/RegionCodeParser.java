package com.antock.task.service.externalapi.regioncode;

import java.io.UnsupportedEncodingException;

public interface RegionCodeParser {
    String parse(String businessAddress) throws UnsupportedEncodingException;
}
