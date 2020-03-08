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


import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.types.*;

public class StructReader
{
	public static void read(MmdColor4 i_dest,DataReader i_reader)  throws MmdException
	{
		i_dest.r=i_reader.readFloat();
		i_dest.g=i_reader.readFloat();
		i_dest.b=i_reader.readFloat();
		i_dest.a=i_reader.readFloat();
		return;
	}
	public static void read(MmdColor3 i_dest,DataReader i_reader)  throws MmdException
	{
		i_dest.r=i_reader.readFloat();
		i_dest.g=i_reader.readFloat();
		i_dest.b=i_reader.readFloat();
		return;
	}
	public static void read(MmdTexUV i_dest,DataReader i_reader)  throws MmdException
	{
		i_dest.u=i_reader.readFloat();
		i_dest.v=i_reader.readFloat();
		return;
	}
	public static void read(MmdVector3 i_dest,DataReader i_reader)  throws MmdException
	{
		i_dest.x=i_reader.readFloat();
		i_dest.y=i_reader.readFloat();
		i_dest.z=i_reader.readFloat();
		return;
	}	
	public static void read(MmdVector4 i_dest,DataReader i_reader) throws MmdException
	{
		i_dest.x=i_reader.readFloat();
		i_dest.y=i_reader.readFloat();
		i_dest.z=i_reader.readFloat();
		i_dest.w=i_reader.readFloat();
		return;
	}	
}
