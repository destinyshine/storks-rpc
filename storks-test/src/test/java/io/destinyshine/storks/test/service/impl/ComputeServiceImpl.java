package io.destinyshine.storks.test.service.impl;

import io.destinyshine.storks.spring.boot.StorksProvider;
import io.destinyshine.storks.test.service.api.ComputeService;
import org.springframework.stereotype.Service;

@StorksProvider
@Service
public class ComputeServiceImpl implements ComputeService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

}
