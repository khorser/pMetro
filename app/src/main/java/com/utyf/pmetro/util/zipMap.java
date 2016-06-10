package com.utyf.pmetro.util;

import com.utyf.pmetro.MapActivity;
import com.utyf.pmetro.settings.SET;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Utyf on 03.03.2015.
 *
 */

public class zipMap {

    static class mapEntry {
        String name;
        long   time;
        int    size;
        byte[] content;
    }

    static ArrayList<mapEntry> map = new ArrayList<>();

    static public boolean load() {
        int            count;
        final int      bufferSize=102400;
        byte[]         bb = new byte[bufferSize];
        ZipInputStream zis;
        ZipEntry       ze;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapEntry       me;

        map.clear();

        if( SET.mapFile==null || SET.mapFile.isEmpty() ) return false;
        String  zFile = MapActivity.catalogDir+"/"+SET.mapFile;

        try {
            zis = new ZipInputStream( new BufferedInputStream(new FileInputStream(zFile)) );

            while( (ze=zis.getNextEntry()) != null )  {   // get all files in archive
                me = new mapEntry();
                me.name = ze.getName();
                if( me.name.toLowerCase().endsWith(".pm3d") ) continue;  // skip 3D files
                me.time = ze.getTime();
                me.size = (int) ze.getSize();
                baos.reset();
                while( (count=zis.read(bb)) > 0 )         // read file to buffer
                    baos.write(bb, 0, count);
                me.content = baos.toByteArray();
                map.add(me);
                }
            zis.close();

        } catch (IOException e) {
            MapActivity.errorMessage = e.toString();
            map.clear();
            return false;
        }

        return true;
    }

    static public byte[] getFile(String fileName ){
        for( mapEntry me : map )
            if( me.name.toLowerCase().equals(fileName.toLowerCase()) ) return me.content;
        return null;
    }

    static public String[] getFileList(String ext)  {
        ArrayList<String> names = new ArrayList<>();
        for (mapEntry entry: map) {
            if (entry.name.toLowerCase().endsWith(ext))
                names.add(entry.name);
        }
        return names.toArray(new String[names.size()]);
    }
}
