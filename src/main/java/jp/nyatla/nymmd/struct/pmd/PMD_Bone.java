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
import jp.nyatla.nymmd.types.MmdVector3;

public class PMD_Bone implements StructType
{	
	public String szName;			// ボーン名 (0x00 終端，余白は 0xFD)
	public int	nParentNo;			// 親ボーン番号 (なければ -1)
	public int	nChildNo;			// 子ボーン番号
	public int	cbKind;		// ボーンの種類
	public int	unIKTarget;	// IK時のターゲットボーン
	public final MmdVector3	vec3Position=new MmdVector3();	// モデル原点からの位置
	
	public void read(DataReader i_reader) throws MmdException
	{
		//szName
		this.szName=i_reader.readAscii(20);
		this.nParentNo=i_reader.readShort();
		this.nChildNo=i_reader.readShort();
		this.cbKind=i_reader.readByte();
		this.unIKTarget=i_reader.readShort();
		StructReader.read(this.vec3Position, i_reader);
		return;
	}
}
