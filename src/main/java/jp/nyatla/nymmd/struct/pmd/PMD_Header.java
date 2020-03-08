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
package jp.nyatla.nymmd.struct.pmd;

import jp.nyatla.nymmd.*;
import jp.nyatla.nymmd.struct.*;

public class PMD_Header implements StructType
{
	public final static int SIZE_OF_STRUCT=3+4+20+256;
	public String szMagic;
	public float fVersion;
	public String szName;
	public String szComment;
	
	public void read(DataReader i_reader) throws MmdException
	{
		this.szMagic=i_reader.readAscii(3);
		//
		this.fVersion=i_reader.readFloat();
		//szName
		this.szName=i_reader.readAscii(20);

		//szComment
		this.szComment=i_reader.readAscii(256);
		return;
	}
/*
	char	szMagic[3];		// "Pmd"
	float	fVersion;		// PMDバージョン番号
	char	szName[20];		// モデル名
	char	szComment[256];	// コメント(著作権表示など)
*/
}
