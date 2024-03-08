package io.littlehorse.verifications;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.verifications.data.Passport;
import io.littlehorse.verifications.data.Verification;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class Tasks {
  @LHTaskMethod("process-passport")
  public Verification processPassport(Passport passport) throws Exception {
    // This method should process the passport data
    // in this implementation it simply rely on a `legit` property receive from the request
    log.info("Verifications passport: %s", passport);
    return new Verification(passport.getLegit());
  }
}
