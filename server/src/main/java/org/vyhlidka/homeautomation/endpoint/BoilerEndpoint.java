package org.vyhlidka.homeautomation.endpoint;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.repo.BoilerRepository;

import java.util.Set;

/**
 * Created by lucky on 18.12.16.
 */
@RestController
@RequestMapping(value = "/boilers", produces = MediaType.APPLICATION_JSON_VALUE)
public class BoilerEndpoint {

    private final BoilerRepository boilerRepository;

    @Autowired
    public BoilerEndpoint(final BoilerRepository boilerRepository) {
        Validate.notNull(boilerRepository, "boilerRepository can not be null;");
        this.boilerRepository = boilerRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<String> getBoilers() {
        return this.boilerRepository.getBoilerKeys();
    }

    @RequestMapping("/{id}")
    public ResponseEntity<Boiler> getBoiler(@PathVariable(name = "id") String id) {
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.badRequest().body(null);
        }

        Boiler b = this.boilerRepository.getBoiler(id);
        return ResponseEntity.ok(b);
    }

}
