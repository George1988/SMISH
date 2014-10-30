#include <jni.h>
#include "aes.c"

#ifndef NULL
 #define NULL 0
 #endif

#ifndef _Included_cnsh_Encrypt
#define _Included_cnsh_Encrypt
#ifdef __cplusplus
extern "C" {
#endif

//jbytearray to char*
void jnijbytearrytochar( JNIEnv* pEnv , jbyteArray strIn , unsigned char ** ppData )
{

	jbyte * olddata = (jbyte*)pEnv->GetByteArrayElements(strIn, 0);
	jsize  oldsize = pEnv->GetArrayLength(strIn);

	int len = (int)oldsize;
	*ppData = new unsigned char [ len ] ;

	if ( *ppData == NULL )
	{
		return ;
	}

	memcpy(  *ppData, olddata , len ) ;
	pEnv->ReleaseByteArrayElements( strIn , olddata , 0 );
}

jbyteArray jnichartojbyteArray( JNIEnv* pEnv , unsigned char* pData ,int size)
{
	if (pEnv == NULL) {

	}

	jbyteArray bytes = pEnv->NewByteArray(size);
	pEnv->SetByteArrayRegion(bytes, 0, size , (jbyte*) pData);

	return bytes;
}

/*
 * Class:     cnsh_Encrypt
 * Method:    decrypt
 * Signature: ([BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_shsmi_map_Encrypt_decrypt
  (JNIEnv *env, jclass, jbyteArray iconData, jint size)
{
	unsigned char *pBuf = NULL ;
	jnijbytearrytochar( env , iconData , &pBuf );
	// Ê¹ÓÃpuf

	unsigned char *pRe = decrypt(pBuf, size);

	jbyteArray  bytearray = jnichartojbyteArray(env , pRe + 1 , size - (int)pRe[0] );

	delete pBuf;
	delete pRe;

	return bytearray ;
}

#ifdef __cplusplus
}
#endif
#endif
