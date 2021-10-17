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

import jp.nyatla.nymmd.struct.DataReader;
import jp.nyatla.nymmd.struct.vmd.VMD_Face;
import jp.nyatla.nymmd.struct.vmd.VMD_Header;
import jp.nyatla.nymmd.struct.vmd.VMD_Motion;
import jp.nyatla.nymmd.types.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

//------------------------------
//ボーンキーフレームソート用比較関数
//------------------------------
class BoneCompare implements Comparator<BoneKeyFrame>
{
	public int compare(BoneKeyFrame o1, BoneKeyFrame o2)
	{
		return (int)(o1.fFrameNo - o2.fFrameNo);
	}
}


//------------------------------
//表情キーフレームソート用比較関数
//------------------------------
class FaceCompare implements Comparator<FaceKeyFrame>
{
	public int compare(FaceKeyFrame o1, FaceKeyFrame o2)
	{
		return (int)(o1.fFrameNo - o2.fFrameNo);
	}
}

public class MmdVmdMotion_BasicClass
{
	private MotionData[] _motion_data_array;	// ボーンごとのキーフレームデータのリスト
	private FaceData[] _face_data_array;	// 表情ごとのキーフレームデータのリスト
	private float _fMaxFrame;		// 最後のフレーム番号

	public MmdVmdMotion_BasicClass(InputStream i_stream) throws MmdException
	{
		initialize(i_stream);
		return;
	}

	public MotionData[] refMotionDataArray()
	{
		return this._motion_data_array;
	}
	public FaceData[] refFaceDataArray()
	{
		return this._face_data_array;
	}

	public float getMaxFrame()
	{
		return this._fMaxFrame;
	}
	

	private boolean initialize(InputStream i_st) throws MmdException
	{
		if(i_st == null){

			VMD_Header tmp_vmd_header=new VMD_Header();

			//ボーンと最大フレームを取得
			float[] max_frame=new float[1];
			this._motion_data_array= new MotionData[2];
			this._motion_data_array[0]=new MotionData();
			this._motion_data_array[0].szBoneName = "すべての親";
			this._motion_data_array[0].ulNumKeyFrames = 1;
			this._motion_data_array[0].pKeyFrames = new BoneKeyFrame[1];
			this._motion_data_array[0].pKeyFrames[0] = new BoneKeyFrame();
			this._motion_data_array[0].pKeyFrames[0].fFrameNo = 1.0f;
			this._motion_data_array[0].pKeyFrames[0].vec3Position.setValue(new MmdVector3(0,0,0));
			this._motion_data_array[0].pKeyFrames[0].vec4Rotate.setValue(new MmdVector4(){{x=0;y=0;z=0;w=1;}});

			this._motion_data_array[1]=new MotionData();
			this._motion_data_array[1].szBoneName = "右足ＩＫ";
			this._motion_data_array[1].ulNumKeyFrames = 3;
			this._motion_data_array[1].pKeyFrames = new BoneKeyFrame[3];
			this._motion_data_array[1].pKeyFrames[0] = new BoneKeyFrame();
			this._motion_data_array[1].pKeyFrames[0].fFrameNo = 0.0f;
			this._motion_data_array[1].pKeyFrames[0].vec3Position.setValue(new MmdVector3(0,5,5));
			this._motion_data_array[1].pKeyFrames[0].vec4Rotate.setValue(new MmdVector4(){{x=0;y=0;z=0;w=1;}});

			this._motion_data_array[1].pKeyFrames[1] = new BoneKeyFrame();
			this._motion_data_array[1].pKeyFrames[1].fFrameNo = 29.0f;
			this._motion_data_array[1].pKeyFrames[1].vec3Position.setValue(new MmdVector3(0,-5,0));
			this._motion_data_array[1].pKeyFrames[1].vec4Rotate.setValue(new MmdVector4(){{x=0;y=0;z=0;w=1;}});

			this._motion_data_array[1].pKeyFrames[2] = new BoneKeyFrame();
			this._motion_data_array[1].pKeyFrames[2].fFrameNo = 59.0f;
			this._motion_data_array[1].pKeyFrames[2].vec3Position.setValue(new MmdVector3(0,5,-5));
			this._motion_data_array[1].pKeyFrames[2].vec4Rotate.setValue(new MmdVector4(){{x=0;y=0;z=0;w=1;}});


			//表情と最大フレームを再取得
			this._face_data_array= new FaceData[0];

			this._fMaxFrame= 60.0f;
			return true;
		}
		
		DataReader reader=new DataReader(i_st);

		// ヘッダのチェック
		VMD_Header tmp_vmd_header=new VMD_Header();
		tmp_vmd_header.read(reader);
		if(!tmp_vmd_header.szHeader.equalsIgnoreCase("Vocaloid Motion Data 0002"))
		{
			throw new MmdException();
		}
		//ボーンと最大フレームを取得
		float[] max_frame=new float[1];
		this._motion_data_array=createMotionDataList(reader,max_frame);
		this._fMaxFrame=max_frame[0];
		
		//表情と最大フレームを再取得
		this._face_data_array=createFaceDataList(reader,max_frame);
		this._fMaxFrame=this._fMaxFrame>max_frame[0]?this._fMaxFrame:max_frame[0];
		
		return true;
	}
	
	private static FaceData[] createFaceDataList(DataReader i_reader,float[] o_max_frame) throws MmdException
	{
		//-----------------------------------------------------
		// 表情のキーフレーム数を取得
		Vector<FaceData> result=new Vector<FaceData>();
		int ulNumFaceKeyFrames=i_reader.readInt();	

		//規定フレーム数分表情を読み込み
		VMD_Face[] tmp_vmd_face=new VMD_Face[ulNumFaceKeyFrames];
		for(int i=0;i<ulNumFaceKeyFrames;i++){
			tmp_vmd_face[i]= new VMD_Face();
			tmp_vmd_face[i].read(i_reader);
		}
		float max_frame=0.0f;
		for(int i = 0 ; i < ulNumFaceKeyFrames ; i++)
		{
			if(max_frame < (float)tmp_vmd_face[i].ulFrameNo ){
				max_frame = (float)tmp_vmd_face[i].ulFrameNo;	// 最大フレーム更新
			}
			boolean is_found=false;
			for(int i2=0;i2<result.size();i2++)
			{
				final FaceData pFaceTemp = result.get(i2);
				if(pFaceTemp.szFaceName.equals(tmp_vmd_face[i].szFaceName))
				{
					// リストに追加済み
					pFaceTemp.ulNumKeyFrames++;
					is_found=true;
					break;
				}
			}

			if(!is_found)
			{
				// リストにない場合は新規ノードを追加
				FaceData pNew = new FaceData();
				pNew.szFaceName=tmp_vmd_face[i].szFaceName;
				pNew.ulNumKeyFrames = 1;
				result.add(pNew);
			}
		}

		// キーフレーム配列を確保
		for(int i=0;i<result.size();i++)
		{
			FaceData pFaceTemp=result.get(i);
			pFaceTemp.pKeyFrames = FaceKeyFrame.createArray(pFaceTemp.ulNumKeyFrames);
			pFaceTemp.ulNumKeyFrames = 0;		// 配列インデックス用にいったん0にする
		}
		
		// 表情ごとにキーフレームを格納
		for(int i = 0 ; i < ulNumFaceKeyFrames ; i++)
		{
			for(int i2=0;i2<result.size();i2++)
			{
				FaceData pFaceTemp = result.get(i2);
				if(pFaceTemp.szFaceName.equals(tmp_vmd_face[i].szFaceName))
				{
					FaceKeyFrame pKeyFrame = pFaceTemp.pKeyFrames[pFaceTemp.ulNumKeyFrames];

					pKeyFrame.fFrameNo = (float)tmp_vmd_face[i].ulFrameNo;
					pKeyFrame.fRate    =        tmp_vmd_face[i].fFactor;

					pFaceTemp.ulNumKeyFrames++;
					break;
				}
			}
		}

		// キーフレーム配列を昇順にソート
		for(int i=0;i<result.size();i++)
		{
			FaceData pFaceTemp = result.get(i);
			Arrays.sort(pFaceTemp.pKeyFrames, new FaceCompare());
		}
		o_max_frame[0]=max_frame;
		return result.toArray(new FaceData[result.size()]);
	}
	private static MotionData[] createMotionDataList(DataReader i_reader,float[] o_max_frame) throws MmdException
	{	
		Vector<MotionData> result=new Vector<MotionData>();
		// まずはモーションデータ中のボーンごとのキーフレーム数をカウント
		final int ulNumBoneKeyFrames=i_reader.readInt();


		//ボーンを指定数読み込み
		VMD_Motion[] tmp_vmd_motion=new VMD_Motion[ulNumBoneKeyFrames];
		for(int i=0;i<ulNumBoneKeyFrames;i++){
			tmp_vmd_motion[i]= new VMD_Motion();
			tmp_vmd_motion[i].read(i_reader);
		}		
		
		float max_frame=0.0f;

		for(int i = 0 ; i < ulNumBoneKeyFrames ; i++)
		{
			if( max_frame < tmp_vmd_motion[i].ulFrameNo){
				max_frame = tmp_vmd_motion[i].ulFrameNo;	// 最大フレーム更新
			}
			boolean is_found=false;
			for(int i2=0;i2<result.size();i2++)
			{
				final MotionData pMotTemp = result.get(i2);
				if(pMotTemp.szBoneName.equals(tmp_vmd_motion[i].szBoneName))
				{
					// リストに追加済みのボーン
					pMotTemp.ulNumKeyFrames++;
					is_found=true;
					break;
				}
			}
			
			if(!is_found)
			{
				// リストにない場合は新規ノードを追加
				MotionData pNew = new MotionData();
				pNew.szBoneName=tmp_vmd_motion[i].szBoneName;
				pNew.ulNumKeyFrames = 1;
				result.add(pNew);
			}
		}

		
		// キーフレーム配列を確保
		for(int i=0;i<result.size();i++)
		{
			final MotionData pMotTemp = result.get(i);
			pMotTemp.pKeyFrames = BoneKeyFrame.createArray(pMotTemp.ulNumKeyFrames);
			pMotTemp.ulNumKeyFrames = 0;		// 配列インデックス用にいったん0にする
		}
		
		// ボーンごとにキーフレームを格納
		for(int i = 0 ; i < ulNumBoneKeyFrames ; i++)
		{
			for(int i2=0;i2<result.size();i2++)
			{
				final MotionData pMotTemp = result.get(i2);
				if(pMotTemp.szBoneName.equals(tmp_vmd_motion[i].szBoneName))
				{
					final BoneKeyFrame pKeyFrame = pMotTemp.pKeyFrames[pMotTemp.ulNumKeyFrames];

					pKeyFrame.fFrameNo     = (float)tmp_vmd_motion[i].ulFrameNo;
					pKeyFrame.vec3Position.setValue(tmp_vmd_motion[i].vec3Position);
					pKeyFrame.vec4Rotate.QuaternionNormalize(tmp_vmd_motion[i].vec4Rotate);

					pMotTemp.ulNumKeyFrames++;

					break;
				}
			}
		}

		// キーフレーム配列を昇順にソート

		for(int i=0;i<result.size();i++)
		{
			final MotionData pMotTemp = result.get(i);
			Arrays.sort(pMotTemp.pKeyFrames, new BoneCompare());
		}
		
		o_max_frame[0]=max_frame;
		return result.toArray(new MotionData[result.size()]);
		
	}
}
