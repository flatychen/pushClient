package cn.flaty.pushFrame;

import cn.flaty.utils.AssertUtils;

public class SimplePushFrame {

	private FrameHead frameHead;
	
	private byte[] head;
	
	private byte[] body;

	public SimplePushFrame(FrameHead frameHead, byte[] frame) {
		super();
		this.frameHead = frameHead;
		this.init(frame);
		this.frameHead = frameHead;
	}

	
	private void init(byte[] frame) {
		if( frame.length <= frameHead.byteLength()){
			throw new IllegalArgumentException("----> frame 内容不能为空 ");
		}
		
		head = new byte[frameHead.headLength()];
		body = new byte[frame.length - frameHead.headLength() ];
		
		System.arraycopy(frame, 0, head, 0, frameHead.headLength());
		System.arraycopy(frame, frameHead.headLength(), body, 0, frame.length - frameHead.headLength());
	}
	
	public SimplePushFrame(FrameHead frameHead, byte[] head, byte[] body) {
		super();
		this.frameHead = frameHead;
		this.head = head;
		this.body = body;
	}
	

	public byte getEncypeType(){
		AssertUtils.notNull(head, "----> 包头不能为空");
		return head[0];
	}

	public byte getCharsetType(){
		AssertUtils.notNull(head, "----> 包头不能为空");
		return head[1];
	}
	
	
	public byte[] getBody() {
		return body;
	}

	
	
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	

}
