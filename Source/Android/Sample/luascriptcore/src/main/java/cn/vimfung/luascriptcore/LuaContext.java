package cn.vimfung.luascriptcore;

import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Lua上下文对象
 * Created by vimfung on 16/8/22.
 */
public class LuaContext extends LuaBaseObject
{
    private Context _context;

    /**
     * 创建上下文对象
     * @param nativeId  本地对象标识
     */
    protected LuaContext(int nativeId)
    {
        super(nativeId);
    }

    /**
     * 创建上下文对象
     * @return  上下文对象
     */
    public static LuaContext create(Context context)
    {
        LuaContext luaContext = LuaNativeUtil.createContext();
        luaContext._context = context;

        return luaContext;
    }

    /**
     * 解析Lua脚本
     * @param script  脚本
     * @return 执行后返回的值
     */
    public LuaValue evalScript (String script)
    {
        return LuaNativeUtil.evalScript(_nativeId, script);
    }

    /**
     * 解析Lua脚本文件
     * @param filePath  Lua文件路径
     * @return  执行后返回值
     */
    public LuaValue evalScriptFromFile (String filePath)
    {
        LuaValue retValue = null;

        String AssetsPathPrefix = "file:///android_asset";
        if (filePath.startsWith(AssetsPathPrefix))
        {
            //asssets目录文件，需要先拷贝到手机目录下再执行解析
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            try
            {
                //创建临时文件
                File tmpFile = File.createTempFile("eval_lua_",".lua", _context.getExternalCacheDir());

                readAssetFileContent(filePath.substring(AssetsPathPrefix.length() + 1), dataStream);
                writeToFile(tmpFile, dataStream);
                dataStream.close();

                retValue = LuaNativeUtil.evalScriptFromFile(_nativeId, tmpFile.toString());

                tmpFile.delete();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            retValue = LuaNativeUtil.evalScriptFromFile(_nativeId, filePath);
        }

        if (retValue == null)
        {
            retValue = new LuaValue();
        }

        return retValue;
    }

    /**
     * 调用Lua的方法
     * @param methodName    方法名称
     * @param arguments     参数列表
     * @return  返回值
     */
    public LuaValue callMethod(String methodName, LuaValue[] arguments)
    {
        return LuaNativeUtil.callMethod(_nativeId, methodName, arguments);
    }

    /**
     * 读取资源文件内容
     * @param fileName  文件名称
     * @param outputStream  输出内容的二进制流
     */
    private void readAssetFileContent(String fileName, ByteArrayOutputStream outputStream)
    {
        try
        {
            InputStream stream = _context.getAssets().open(fileName);
            byte[] buffer = new byte[1024];

            int hasRead = 0;
            while ((hasRead = stream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, hasRead);
            }

            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件
     * @param file  目标文件
     * @param dataStream    二进制流
     */
    private void writeToFile(File file, ByteArrayOutputStream dataStream)
    {
        try
        {
            FileOutputStream stream = new FileOutputStream(file);
            dataStream.writeTo(stream);
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}