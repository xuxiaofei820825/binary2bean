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

import com.github.binary2bean.annotation.NBits;

import lombok.Data;

public class BitsetNumberFieldDecoderTest {

	private static final String TEST_FILE = "TEST_FILE";
	private static final byte[] BYTES = new byte[] { 0b00101011, 0b00101011 };

	@Before
	public void setUp() throws Exception {
		File file = new File( TEST_FILE );

		OutputStream os = new FileOutputStream( file );

		os.write( BYTES );
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
		BitsNumberFieldBean rs = decoder.decode( BitsNumberFieldBean.class );

		Assert.assertEquals( 21, rs.getN1() );
		Assert.assertEquals( 9, rs.getN2() );
		Assert.assertEquals( 11, rs.getN3() );

		aFile.close();
	}

	@Data
	static class BitsNumberFieldBean {

		@NBits(bytes = 2, offset = 0, len = 7)
		private Number n1;

		@NBits(bytes = 2, offset = 7, len = 4)
		private Number n2;

		@NBits(bytes = 2, offset = 11, len = 5)
		private Number n3;
	}
}
