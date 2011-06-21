package com.openfeint.internal.request;

import java.io.IOException;

import com.openfeint.api.R;
import com.openfeint.internal.OpenFeintInternal;

public abstract class CompressedBlobDownloadRequest extends DownloadRequest {

	@Override
	protected final void onSuccess(byte[] body) {
		try {
			onSuccessDecompress(Compression.decompress(body));
		} catch (IOException e) {
			onFailure(OpenFeintInternal.getRString(R.string.of_io_exception_on_download));
		}
	}
	
	abstract protected void onSuccessDecompress(byte decompressedBody[]);
}
