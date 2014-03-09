package com.letv.shop.aladdin.server.warner;

import java.util.List;

public interface Warner {
	void warn(List<String> receiver, String content);
}
