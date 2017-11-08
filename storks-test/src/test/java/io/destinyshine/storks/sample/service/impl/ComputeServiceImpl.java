package io.destinyshine.storks.sample.service.impl;

import io.destinyshine.storks.sample.service.api.ComputeService;

public class ComputeServiceImpl implements ComputeService {

    @Override
    public int sum(int a, int b) {
        return a + b;
    }

}
