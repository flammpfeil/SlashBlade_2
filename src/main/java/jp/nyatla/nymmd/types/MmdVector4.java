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
package jp.nyatla.nymmd.types;


public class MmdVector4
{
	public double x, y, z, w;		
	public void setValue(MmdVector4 v)
	{
		this.x=v.x;
		this.y=v.y;
		this.z=v.z;
		this.w=v.w;
		return;
	}	
	public void QuaternionSlerp(MmdVector4 pvec4Src1,MmdVector4 pvec4Src2, double fLerpValue )
	{

		// Qlerp
		double	qr = pvec4Src1.x * pvec4Src2.x + pvec4Src1.y * pvec4Src2.y + pvec4Src1.z * pvec4Src2.z + pvec4Src1.w * pvec4Src2.w;
		double	t0 = 1.0f - fLerpValue;

		if( qr < 0 )
		{
			this.x = pvec4Src1.x * t0 - pvec4Src2.x * fLerpValue;
			this.y = pvec4Src1.y * t0 - pvec4Src2.y * fLerpValue;
			this.z = pvec4Src1.z * t0 - pvec4Src2.z * fLerpValue;
			this.w = pvec4Src1.w * t0 - pvec4Src2.w * fLerpValue;
		}
		else
		{
			this.x = pvec4Src1.x * t0 + pvec4Src2.x * fLerpValue;
			this.y = pvec4Src1.y * t0 + pvec4Src2.y * fLerpValue;
			this.z = pvec4Src1.z * t0 + pvec4Src2.z * fLerpValue;
			this.w = pvec4Src1.w * t0 + pvec4Src2.w * fLerpValue;
		}
		QuaternionNormalize(this);
		return;
	}
	public void QuaternionNormalize(MmdVector4 pvec4Src)
	{
		final double fSqr =1.0 / Math.sqrt(( pvec4Src.x * pvec4Src.x + pvec4Src.y * pvec4Src.y + pvec4Src.z * pvec4Src.z + pvec4Src.w * pvec4Src.w));

		this.x =(pvec4Src.x * fSqr);
		this.y =(pvec4Src.y * fSqr);
		this.z =(pvec4Src.z * fSqr);
		this.w =(pvec4Src.w * fSqr);
	}
	public void QuaternionCreateAxis(MmdVector3 pvec3Axis, double fRotAngle )
	{
		if( Math.abs( fRotAngle ) < 0.0001f )
		{
			this.x = this.y = this.z = 0.0f;
			this.w = 1.0f;
		}
		else
		{
			fRotAngle *= 0.5f;
			double	fTemp = Math.sin(fRotAngle);

			this.x = pvec3Axis.x * fTemp;
			this.y = pvec3Axis.y * fTemp;
			this.z = pvec3Axis.z * fTemp;
			this.w = Math.cos( fRotAngle );
		}
		return;
	}
	public void QuaternionMultiply(MmdVector4 pvec4Src1,MmdVector4 pvec4Src2)
	{
		double	px, py, pz, pw;
		double	qx, qy, qz, qw;

		px = pvec4Src1.x; py = pvec4Src1.y; pz = pvec4Src1.z; pw = pvec4Src1.w;
		qx = pvec4Src2.x; qy = pvec4Src2.y; qz = pvec4Src2.z; qw = pvec4Src2.w;

		this.x = pw * qx + px * qw + py * qz - pz * qy;
		this.y = pw * qy - px * qz + py * qw + pz * qx;
		this.z = pw * qz + px * qy - py * qx + pz * qw;
		this.w = pw * qw - px * qx - py * qy - pz * qz;
	}
	public void QuaternionCreateEuler(MmdVector3 pvec3EulerAngle )
	{
		final double	xRadian = pvec3EulerAngle.x * 0.5;
		final double	yRadian = pvec3EulerAngle.y * 0.5;
		final double	zRadian = pvec3EulerAngle.z * 0.5;
		final double	sinX = Math.sin( xRadian );
		final double	cosX = Math.cos( xRadian );
		final double	sinY = Math.sin( yRadian );
		final double	cosY = Math.cos( yRadian );
		final double	sinZ = Math.sin( zRadian );
		final double	cosZ = Math.cos( zRadian );

		// XYZ
		this.x = sinX * cosY * cosZ - cosX * sinY * sinZ;
		this.y = cosX * sinY * cosZ + sinX * cosY * sinZ;
		this.z = cosX * cosY * sinZ - sinX * sinY * cosZ;
		this.w = cosX * cosY * cosZ + sinX * sinY * sinZ;
	}
	
}
