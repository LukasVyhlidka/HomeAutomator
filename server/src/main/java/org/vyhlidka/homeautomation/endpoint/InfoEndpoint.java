package org.vyhlidka.homeautomation.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.vyhlidka.homeautomation.service.BoilerUpdater;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class InfoEndpoint {

    private final BoilerUpdater updater;

    @Autowired
    public InfoEndpoint(final BoilerUpdater updater) {
        this.updater = updater;
    }

    @RequestMapping(method = RequestMethod.GET)
    public InfoRO getInfo() {
        InfoRO info = new InfoRO();

        LocalDateTime lastUpdateTime = this.updater.getLastUpdateTime();
        info.setSystemTime(LocalDateTime.now());
        info.setLastUpadeTime(lastUpdateTime);
        info.setDeltaSec(ChronoUnit.SECONDS.between(lastUpdateTime, LocalDateTime.now()));

        return info;
    }

    private static class InfoRO {

        private LocalDateTime systemTime;

        private LocalDateTime lastUpadeTime;

        private Long deltaSec;

        public LocalDateTime getSystemTime() {
            return systemTime;
        }

        public void setSystemTime(final LocalDateTime systemTime) {
            this.systemTime = systemTime;
        }

        public LocalDateTime getLastUpadeTime() {
            return lastUpadeTime;
        }

        public void setLastUpadeTime(final LocalDateTime lastUpadeTime) {
            this.lastUpadeTime = lastUpadeTime;
        }

        public Long getDeltaSec() {
            return deltaSec;
        }

        public void setDeltaSec(final Long deltaSec) {
            this.deltaSec = deltaSec;
        }
    }

}
