package com.letv.shop.aladdin.server.warner.impl;

import java.util.List;

import com.letv.shop.aladdin.server.warner.Warner;

public class MailWarner implements Warner {

	@Override
	public void warn(List<String> receiver, String content) {
//		try {
//			MailUtil.send(receiver, null, "ALADDIN报警", content, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
