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


import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.struct.*;

public class PMD_FACE implements StructType
{
	public String szName;		// 表情名 (0x00 終端，余白は 0xFD)
	public int	ulNumVertices;	// 表情頂点数
	public int cbType;			// 分類
	public PMD_FACE_VTX	[] pVertices=PMD_FACE_VTX.createArray(64);// 表情頂点データ
	public void read(DataReader i_reader) throws MmdException
	{
		int i;
		//szName
		this.szName=i_reader.readAscii(20);
		this.ulNumVertices=i_reader.readInt();
		this.cbType=i_reader.read();
		//必要な数だけ配列を確保しなおす。
		if(this.ulNumVertices>this.pVertices.length){
			this.pVertices=PMD_FACE_VTX.createArray(this.ulNumVertices);
		}
		for(i=0;i<this.ulNumVertices;i++){
			this.pVertices[i].read(i_reader);
		}
		return;
	}	
/*	
	char			szName[20];		// 表情名 (0x00 終端，余白は 0xFD)

	unsigned long	ulNumVertices;	// 表情頂点数
	unsigned char	cbType;			// 分類

	PMD_FACE_VTX	pVertices[1];	// 表情頂点データ
*/
}
