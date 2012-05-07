package com.scoreloop.client.android.core.demo.labs;

import com.scoreloop.client.android.core.controller.PaymentProviderController;
import com.scoreloop.client.android.core.controller.PaymentProviderControllerObserver;


class BlockingPaymentControllerObserver extends AbstractBlockingControllerObserver implements PaymentProviderControllerObserver {

    @Override
    public void paymentControllerDidCancel(PaymentProviderController paymentProviderController) {
        // mark as received response
        receivedResponse();
    }

    @Override
    public void paymentControllerDidFail(PaymentProviderController paymentProviderController, Exception error) {
        // save any exceptions
        setException(error);
        // mark as received response
        receivedResponse();
    }

    @Override
    public void paymentControllerDidSucceed(PaymentProviderController paymentProviderController) {
        // mark as received response
        receivedResponse();
    }

    @Override
    public void paymentControllerDidFinishWithPendingPayment(PaymentProviderController paymentProviderController) {
        // mark as received response
        receivedResponse();
    }

}
