/*************************************************************************
 *
 * Copyright (c) 2016, DATAVISOR, INC.
 * All rights reserved.
 * __________________
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of DataVisor, Inc.
 * The intellectual and technical concepts contained
 * herein are proprietary to DataVisor, Inc. and
 * may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from DataVisor, Inc.
 */

package com.datavisor.sdkwriter;

import com.datavisor.sdkwriter.util.SdkUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnitTests {

    private static final String EXPECT_BUCKET_NAME = "com.pagoda.buy-X8UtjLSX-DyPbJJ6f";

    @Test
    public void testGetBucketName() {
        String objectKey = "rawdata_sdk/20190916/rawlog.20190916_033500.com.pagoda.buy-X8UtjLSX-DyPbJJ6f_com.pagoda.buy_transaction.sdkwriter-ae6f798d5c91-StreamThread-1";
        String bucketname = SdkUtil.getSdkBucketNameFromObjectKey(objectKey, "_");
        assertEquals(EXPECT_BUCKET_NAME, bucketname);
    }

    @Test
    public void testInvaildBucketName() {
        String objectKey1 = "20190916/rawlog.20190916_033500.com.pagoda.buy-X8UtjLSX-DyPbJJ6f_com.pagoda.buy_transaction.sdkwriter-ae6f798d5c91-StreamThread-1";
        String bucketname1 = SdkUtil.getSdkBucketNameFromObjectKey(objectKey1, "_");
        assertNull(bucketname1);

        String objectKey2 = "rawdata_sdk/20190916/20190916_033500.com.pagoda.buy-X8UtjLSX-DyPbJJ6f_com.pagoda.buy_transaction.sdkwriter-ae6f798d5c91-StreamThread-1";
        String bucketname2 = SdkUtil.getSdkBucketNameFromObjectKey(objectKey2, "_");
        assertNotEquals(EXPECT_BUCKET_NAME, bucketname2);
    }
}
