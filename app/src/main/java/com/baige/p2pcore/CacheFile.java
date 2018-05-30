package com.baige.p2pcore;

import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by baige on 2018/5/28.
 */

public class CacheFile {

    public final static int EMPTY = 0;
    public final static int DOWNLOAD = 1;
    public final static int FINISH = 2;

    private final static String TAG = CacheFile.class.getSimpleName();
    private FileOutputStream outputStream;
    private RandomAccessFile randomAccessFile;

    private String filePath;
    private String fileName;

    private File file;
    private long fileSize;
    private long fullsize;          //完整文件大小
    private int state;              //文件状态
    private boolean readOnly;

    private boolean isEnable;

    public CacheFile(String path, String name, boolean readOnly) {
        this.filePath = path;
        this.fileName = name;
        this.readOnly = readOnly;
        init();
    }
    private void init(){

        if(readOnly){
            file = new File(getFilePath() + File.separator + getFileName());
            if(!file.exists()){
                isEnable = false;
            }else{
                try {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    setFullsize(file.length());
                    isEnable = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    isEnable = false;
                }
            }
        }else{
            file = new File(getFilePath() + File.separator + getFileName() + ".tmp");
            if(!file.exists()){
                File dir = file.getParentFile();
                dir.mkdirs();
                try {
                    file.createNewFile();
                    getFileSize();
                    outputStream = new FileOutputStream(file);
                    isEnable = true;
                    setState(DOWNLOAD);
                } catch (IOException e) {
                    e.printStackTrace();
                    isEnable = false;
                }
            }else{
                //TODO 利用上缓存
                file.delete();
                try {
                    file.createNewFile();
                    getFileSize();
                    outputStream = new FileOutputStream(file);
                    isEnable = true;
                    setState(DOWNLOAD);
                } catch (IOException e) {
                    e.printStackTrace();
                    isEnable = false;
                }
            }
        }
    }

    public byte[] read(long startPos) {
        if (isEnable && readOnly) {
            int byteCount = (int) (getFileSize() - startPos);
            byte[] tmp = new byte[byteCount];
            try {
                randomAccessFile.seek(startPos);
                randomAccessFile.read(tmp);
                return tmp;
            } catch (IOException e) {
                Log.e(TAG, "缓存读取失败", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public byte[] read(long startPos, int length) {
        if (isEnable && readOnly) {
            int byteCount = (int) (getFileSize() - startPos);
            if (byteCount > length) {
                byteCount = length;
            }
            byte[] tmp = new byte[byteCount];
            try {
                randomAccessFile.seek(startPos);
                randomAccessFile.read(tmp);
                return tmp;
            } catch (IOException e) {
                Log.e(TAG, "缓存读取失败", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean write(byte[] buffer, int byteCount) {
        boolean res = false;
        if (isEnable && !readOnly) {
            try {
                outputStream.write(buffer, 0, byteCount);
                outputStream.flush();
                res = true;
            } catch (IOException e) {
                Log.e(TAG, "缓存写入失败", e);
            }
        }else{
            Log.e(TAG, "文件无法写入isEnable ="+isEnable +", readOnly ="+readOnly);
        }
        return res;
    }

    public void close() {
        if (isEnable) {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    randomAccessFile = null;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    outputStream = null;
                }
            }
            isEnable = false;
        }
    }

    public boolean isFinish() {
        if(getFileSize() <= 0){
            state = EMPTY;
            return false;
        }
        if (state == FINISH) {
            return true;
        }
        //TODO 修改文件名,可能会引起失败，因为文件没有关闭
        if (getFileSize() == getFullsize()) {
            setState(CacheFile.FINISH);
            close();
            file.renameTo(new File(getFilePath() + File.separator + getFileName()));   //改名
            return true;
        }
        return false;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public long getFileSize() {
        if (isEnable) {
            return (fileSize = file.length());
        } else {
            return -1;
        }
    }
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public long getFullsize() {
        return fullsize;
    }

    public void setFullsize(long fullsize) {
        this.fullsize = fullsize;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
