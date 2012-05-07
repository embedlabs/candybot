package com.scoreloop.client.android.core.demo.labs;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;

class BlockingRequestControllerObserver extends AbstractBlockingControllerObserver implements RequestControllerObserver {

    @Override
    public void requestControllerDidFail(RequestController aRequestController, Exception anException) {
        // save any exceptions
        setException(anException);
        // mark as received response
        receivedResponse();
    }

    @Override
    public void requestControllerDidReceiveResponse(RequestController aRequestController) {
        // mark as received response
        receivedResponse();
    }

}
