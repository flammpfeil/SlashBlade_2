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
package jp.nyatla.nymmd.core;

import jp.nyatla.nymmd.struct.pmd.PMD_IK;
import jp.nyatla.nymmd.types.*;

public class PmdIK
{
	private PmdBone m_pTargetBone;	// IKターゲットボーン
	private PmdBone m_pEffBone;	// IK先端ボーン

	private int m_unCount;
	private double _fact;
	private int m_nSortVal;

	private PmdBone[] m_ppBoneList;	// IKを構成するボーンの配列
	
	private final MmdVector3[] _work_vector3=MmdVector3.createArray(4);
	private final MmdVector4 _work_vector4=new MmdVector4();
	public PmdIK(PMD_IK pPMDIKData, PmdBone[] i_ref_bone_array)
	{
		// IKターゲットボーン
		this.m_pTargetBone = i_ref_bone_array[pPMDIKData.nTargetNo];

		// IK先端ボーン
		this.m_pEffBone =i_ref_bone_array[pPMDIKData.nEffNo];

		this.m_unCount = pPMDIKData.unCount;
		this._fact =pPMDIKData.fFact * Math.PI;
		this.m_nSortVal = pPMDIKData.punLinkNo[0];

		// IKリンク配列の作成
		final int number_of_ik_link=pPMDIKData.cbNumLink;
		
		this.m_ppBoneList = new PmdBone[number_of_ik_link];//参照
		for(int i = 0 ; i < number_of_ik_link ; i++ )
		{
			this.m_ppBoneList[i] =i_ref_bone_array[ pPMDIKData.punLinkNo[i] ];	// ボーン番号は降順で格納されている
			if(this.m_ppBoneList[i].getName().equals("左ひざ") || this.m_ppBoneList[i].getName().equals("右ひざ"))
			{
				this.m_ppBoneList[i].setIKLimitAngle(true);
			}
		}			
	}	
	private void limitAngle( MmdVector4 pvec4Out,MmdVector4 pvec4Src )
	{
		final MmdVector3 vec3Angle=this._work_vector3[0];

		// XYZ軸回転の取得
		vec3Angle.QuaternionToEuler(pvec4Src);

		// 角度制限
		if( vec3Angle.x <-Math.PI ){
			vec3Angle.x = (float)-Math.PI;
		}
		if( -0.002f < vec3Angle.x ){
			vec3Angle.x = -0.002f;
		}
		vec3Angle.y = 0.0f;
		vec3Angle.z = 0.0f;

		// XYZ軸回転からクォータニオンへ
		pvec4Out.QuaternionCreateEuler(vec3Angle);
		return;
	}

	public int getSortVal()
	{
		return this.m_nSortVal;
	}
	private final MmdMatrix __update_matInvBone=new MmdMatrix();
	public void update()
	{
		final MmdMatrix matInvBone=this.__update_matInvBone;


		final MmdVector3 vec3EffPos=this._work_vector3[0];
		final MmdVector3 vec3TargetPos=this._work_vector3[1];
		final MmdVector3 vec3Diff=this._work_vector3[2];
		final MmdVector3 vec3RotAxis=this._work_vector3[3];
		final MmdVector4 vec4RotQuat=this._work_vector4;

		//事前に全Boneをupdateしてるなら、このコードは要らない?
		for(int i =this.m_ppBoneList.length - 1 ; i >= 0 ; i-- ){
			this.m_ppBoneList[i].updateMatrix();
		}
		m_pEffBone.updateMatrix();



		for(int it = this.m_unCount-1 ; it >=0  ; it--)
		{
			for(int j = 0 ; j < this.m_ppBoneList.length ; j++ )
			{
				// エフェクタの位置の取得

				// ワールド座標系から注目ノードの局所(ローカル)座標系への変換
				matInvBone.inverse(m_ppBoneList[j].m_matLocal );

				// エフェクタ，到達目標のローカル位置
				vec3EffPos.Vector3Transform(m_pEffBone.m_matLocal, matInvBone );
				vec3TargetPos.Vector3Transform(m_pTargetBone.m_matLocal, matInvBone);

				// 十分近ければ終了

				vec3Diff.Vector3Sub(vec3EffPos,vec3TargetPos);
				if(vec3Diff.Vector3DotProduct(vec3Diff) < 0.0000001f ){
					return;
				}

				// (1) 基準関節→エフェクタ位置への方向ベクトル
				vec3EffPos.Vector3Normalize(vec3EffPos);

				// (2) 基準関節→目標位置への方向ベクトル
				vec3TargetPos.Vector3Normalize(vec3TargetPos);

				// ベクトル (1) を (2) に一致させるための最短回転量（Axis-Angle）
				//
				// 回転角
				double	fRotAngle = Math.acos(vec3EffPos.Vector3DotProduct(vec3TargetPos ) );

				if( 0.00000001 < Math.abs( fRotAngle))
				{
					if( fRotAngle < -this._fact ){
						fRotAngle = -this._fact;
					}else if( this._fact < fRotAngle ){
						fRotAngle = this._fact;
					}

					// 回転軸

					vec3RotAxis.Vector3CrossProduct(vec3EffPos,vec3TargetPos);
					if( vec3RotAxis.Vector3DotProduct(vec3RotAxis) < 0.0000001 ){
						continue;
					}

					vec3RotAxis.Vector3Normalize(vec3RotAxis);

					// 関節回転量の補正
					vec4RotQuat.QuaternionCreateAxis(vec3RotAxis,fRotAngle);

					if( m_ppBoneList[j].m_bIKLimitAngle){
						limitAngle(vec4RotQuat,vec4RotQuat );
					}

					vec4RotQuat.QuaternionNormalize(vec4RotQuat);

					m_ppBoneList[j].m_vec4Rotate.QuaternionMultiply(m_ppBoneList[j].m_vec4Rotate,vec4RotQuat);
					m_ppBoneList[j].m_vec4Rotate.QuaternionNormalize(m_ppBoneList[j].m_vec4Rotate);

					for(int i = j ; i >= 0 ; i-- ){
						m_ppBoneList[i].updateMatrix();
					}
					m_pEffBone.updateMatrix();
				}
			}
		}
		return;
	}
}
