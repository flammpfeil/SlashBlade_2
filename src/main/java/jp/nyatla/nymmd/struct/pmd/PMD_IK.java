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
import jp.nyatla.nymmd.struct.DataReader;
import jp.nyatla.nymmd.struct.StructType;

public class PMD_IK implements StructType
{
	public int nTargetNo;	// IKターゲットボーン番号
	public int nEffNo;		// IK先端ボーン番号
	public int	cbNumLink;	// IKを構成するボーンの数
	public int unCount;
	public float fFact;
	public int[] punLinkNo;// IKを構成するボーンの配列(可変長配列)
	
	public void read(DataReader i_reader) throws MmdException
	{
		this.nTargetNo=i_reader.readShort();
		this.nEffNo=i_reader.readShort();
		this.cbNumLink=i_reader.read();
		this.unCount=i_reader.readUnsignedShort();
		this.fFact=i_reader.readFloat();
		//必要な数だけ配列を確保しなおす。
		this.punLinkNo=new int[this.cbNumLink];
		for(int i=0;i<this.cbNumLink;i++){
			this.punLinkNo[i]=i_reader.readUnsignedShort();
		}
		return;
	}	
	
	
	
/*	
	short			nTargetNo;	// IKターゲットボーン番号
	short			nEffNo;		// IK先端ボーン番号

	unsigned char	cbNumLink;	// IKを構成するボーンの数

	unsigned short	unCount;
	float			fFact;

	unsigned short	punLinkNo[1];// IKを構成するボーンの配列
*/
}
