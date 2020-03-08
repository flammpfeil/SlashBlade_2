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
package jp.nyatla.nymmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;



/**
 * ファイルシステムからPMDファイルを読み込むようにラップした{@link MmdPmdModel_BasicClass}
 */
public class MmdPmdModel extends MmdPmdModel_BasicClass
{
	public MmdPmdModel(String i_pmd_file_path) throws FileNotFoundException, MmdException
	{
		super(new FileInputStream(i_pmd_file_path),new FileResourceProvider(i_pmd_file_path));
	}
	public MmdPmdModel(InputStream i_stream,IResourceProvider i_res_provider) throws MmdException
	{
		super(i_stream,i_res_provider);
	}
	
	protected static class FileResourceProvider implements IResourceProvider
	{
		String _dir;
		public FileResourceProvider(String i_pmd_file_path)
		{
			File f=new File(i_pmd_file_path);//pmdのパス
			this._dir=(f.getParentFile().getPath());
		}
		public InputStream getTextureStream(String i_name) throws MmdException
		{
			try{
				return new FileInputStream(this._dir +"\\"+ i_name);
			}catch(Exception e){
				throw new MmdException(e);
			}
		}
	}
}
