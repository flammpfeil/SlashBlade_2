/* 
 * PROJECT: NyMmd
 * --------------------------------------------------------------------------------
 * The MMD for Java is Java version MMD Motion player class library.
 * NyMmd is modules which removed the ARToolKit origin codes from ARTK_MMD,
 * and was ported to Java. 
 *
 * This is based on the ARTK_MMD v0.1 by PY.
 * http://ppyy.if.land.to/artk_mmd.html
 * py1024<at>gmail.com
 * http://www.nicovideo.jp/watch/sm7398691
 *
 * 
 * The MIT License
 * Copyright (C)2008-2012 nyatla
 * nyatla39<at>gmail.com
 * http://nyatla.jp
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nymmd.struct;

import java.io.*;
import java.nio.*;

import jp.nyatla.nymmd.*;

public class DataReader
{
	private ByteBuffer _buf; 
	public DataReader(InputStream i_stream) throws MmdException
	{
		try{
			//コレなんとかしよう。C#のBinaryReaderみたいに振舞うように。
			int file_len=i_stream.available();
			if(file_len<1){
				file_len=2*1024*1024;
			}
			byte[] buf=new byte[file_len];
			int buf_len=i_stream.read(buf,0,file_len);
			this._buf=ByteBuffer.wrap(buf,0,buf_len);
			this._buf.order(ByteOrder.LITTLE_ENDIAN);
			return;
		}catch(Exception e){
			throw new MmdException();
		}
	}
	public int readByte()
	{
		return this._buf.get();
	}
	public int read()
	{
		int v=this._buf.get();
		return (v>=0)?v:0xff+v;//unsignedに戻す
	}
	public short readShort()
	{
		return this._buf.getShort();
	}
	public int readUnsignedShort()
	{
		int v=this._buf.getShort();
		return (v>=0)?v:0xffff+v;//unsignedに戻す
	}
	public int readInt()
	{
		return this._buf.getInt();
	}
	public float readFloat()
	{
		return this._buf.getFloat();
	}
	public double readDouble()
	{
		return this._buf.getDouble();
	}
	public String readAscii(int i_length) throws MmdException
	{
		try{
		String ret="";
		int len=0;
		byte[] tmp=new byte[i_length];
		int i;
		for(i=0;i<i_length;i++){
			byte b=this._buf.get();
			if(b==0x00){
				i++;
				break;
			}
			tmp[i]=b;
			len++;
		}
		ret=new String(tmp,0,len,"Shift_JIS");
		for(;i<i_length;i++){
			this._buf.get();
		}
		return ret;
		}catch(Exception e){
			throw new MmdException();
		}
	}	
}
