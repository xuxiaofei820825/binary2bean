package com.github.binary2bean.decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.binary2bean.annotation.NBytes;
import com.github.binary2bean.annotation.NumberRange;
import com.github.binary2bean.annotation.Size;
import com.github.binary2bean.annotation.Text;

import lombok.Data;

public class ListPropertyDecoderTest {

	private static final short ListSize = 2;
	private static final String TEST_FILE = "TEST_FILE";

	private static final short RD_1_SIZE = 10;
	private static final String RD_1_TEXT = "RD_1_TEXT";
	private static final short RD_1_NUM = 60;

	private static final short RD_2_SIZE = 20;
	private static final String RD_2_TEXT = "RD_2_TEXT";
	private static final short RD_2_NUM = 88;

	@Before
	public void setUp() throws Exception {
		File file = new File( TEST_FILE );

		OutputStream os = new FileOutputStream( file );

		{ // List的Size
			ByteBuffer buffer = ByteBuffer.allocate( 2 );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( ListSize );
			os.write( buffer.array() );
		}

		// Record_1_start..........
		{// List的Bean

			{ // rdSize
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( RD_1_SIZE );
				os.write( buffer.array() );
			}

			{ // text
				int len = RD_1_TEXT.getBytes().length;
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) len );
				os.write( buffer.array() );
				os.write( RD_1_TEXT.getBytes() );
			}

			{ // number
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( RD_1_NUM );
				os.write( buffer.array() );
			}
		}

		// Record_2_start..........
		{// List的Bean

			{ // rdSize
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( RD_2_SIZE );
				os.write( buffer.array() );
			}

			{ // text
				int len = RD_2_TEXT.getBytes().length;
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) len );
				os.write( buffer.array() );
				os.write( RD_2_TEXT.getBytes() );
			}

			{ // number
				ByteBuffer buffer = ByteBuffer.allocate( 2 );
				buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( RD_2_NUM );
				os.write( buffer.array() );
			}
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
		ListTestBean rs = decoder.decode( ListTestBean.class );

		Assert.assertEquals( ListSize, rs.getRecords().size() );

		Assert.assertEquals( RD_1_TEXT, rs.getRecords().get( 0 ).getName() );
		Assert.assertEquals( RD_1_NUM, rs.getRecords().get( 0 ).getNumber() );

		Assert.assertEquals( RD_2_TEXT, rs.getRecords().get( 1 ).getName() );
		Assert.assertEquals( RD_2_NUM, rs.getRecords().get( 1 ).getNumber() );

		aFile.close();
	}

	@Data
	static class ListTestBean {

		@NBytes(len = 2)
		@Size(max = 255)
		private List<ListRecordBean> records;
	}

	@Data
	static class ListRecordBean {

		@NBytes(len = 2)
		@NumberRange(min = 1, max = 351)
		private Number rdSize;

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name;

		@NBytes(len = 2)
		private Number number;

	}
}
