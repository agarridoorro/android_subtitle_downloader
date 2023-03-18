package com.agorro.subtitledownloader.utils;

import com.agorro.subtitledownloader.smb.AuthInfo;

import java.io.IOException;
import java.util.Properties;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import jcifs.CIFSContext;
import jcifs.Configuration;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;

public class Samba
{
    public static SmbFile getFile(String path, AuthInfo authInfo) throws IOException
    {
        /*NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
                authInfo.getDomain(), authInfo.getUser(), authInfo.getPassword());
        return new SmbFile(path, auth);*/

        BaseContext baseCxt = null;
        Properties jcifsProperties  = new Properties();
        jcifsProperties.setProperty("jcifs.smb.client.enableSMB2", "true");
        jcifsProperties.setProperty("jcifs.smb.client.dfs.disabled","true");
        Configuration config = new PropertyConfiguration(jcifsProperties);
        baseCxt = new BaseContext(config);
        CIFSContext auth = baseCxt.withCredentials(new NtlmPasswordAuthenticator(authInfo.getDomain(), authInfo.getUser(), authInfo.getPassword()));
        return new SmbFile(path, auth);
    }
}
