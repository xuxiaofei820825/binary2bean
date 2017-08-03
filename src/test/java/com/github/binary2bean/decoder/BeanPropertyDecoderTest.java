package com.github.binary2bean.decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.binary2bean.annotation.NBytes;
import com.github.binary2bean.annotation.Text;

import lombok.Data;

public class BeanPropertyDecoderTest {
	
	private static final String TEST_FILE = "TEST_FILE";

	private static final String TEXT1 = "ABCDEFG";
	private static final String TEXT2 = "HIJKLMNOPUYUDSYUDSYUYDSU0199";
	private static final String TEXT3 = "WO5AI3JUAN0JUAN";
	private static final short NUM1 = 3;
	private static final short NUM2 = 688;

	@Before
	public void setUp() throws Exception {
		File file = new File( TEST_FILE );

		OutputStream os = new FileOutputStream( file );

		{ // 写ParentTestBean.name属性
			ByteBuffer buffer = ByteBuffer.allocate( 2 );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) TEXT1.getBytes().length );
			os.write( buffer.array() );
			os.write( TEXT1.getBytes() );
		}

		{// 写ParentTestBean.number属性
			ByteBuffer buffer = ByteBuffer.allocate( 2 );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( NUM1 );
			os.write( buffer.array() );
		}

		{ // 写ParentTestBean.child属性

			{// 写ChildTestBean.name属性
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) TEXT2.getBytes().length );
				os.write( buffer.array() );
				os.write( TEXT2.getBytes() );
			}

			{// 写ChildTestBean.number属性
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( NUM2 );
				os.write( buffer.array() );
			}

			{// 写ChildTestBean.childOfChild属性
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) TEXT3.getBytes().length );
				os.write( buffer.array() );
				os.write( TEXT3.getBytes() );
			}

		}

		{ // 写ParentTestBean.name2属性
			ByteBuffer buffer = ByteBuffer.allocate( 2 );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) TEXT1.getBytes().length );
			os.write( buffer.array() );
			os.write( TEXT1.getBytes() );
		}

		os.flush();
		os.close();
	}

	@After
	public void tearDown() throws Exception {
		File file = new File( TEST_FILE );
		file.delete();
	}

	@Test
	public void test() throws Exception {
		File file = new File( TEST_FILE );

		RandomAccessFile aFile = new RandomAccessFile( file, "rw" );
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer buf = ByteBuffer.allocate( 1024 );
		inChannel.read( buf );

		BeanDecoder decoder = new BeanDecoder( inChannel, buf );
		ParentTestBean rs = decoder.decode( ParentTestBean.class );

		aFile.close();

		Assert.assertEquals( TEXT1, rs.getName() );
		Assert.assertEquals( NUM1, rs.getNumber() );

		Assert.assertEquals( TEXT2, rs.getChild().getName() );
		Assert.assertEquals( NUM2, rs.getChild().getNumber() );

		Assert.assertEquals( TEXT3, rs.getChild().getChildOfChild().getName() );
	}

	@Data
	static class ParentTestBean {

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name;

		@NBytes(len = 2)
		private Number number;

		private ChildTestBean child;

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name2;
	}

	@Data
	static class ChildTestBean {

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name;

		@NBytes(len = 2)
		private Number number;

		private ChildOfChildBean childOfChild;

	}

	@Data
	static class ChildOfChildBean {

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name;

	}
}
