# Fixture: same KEY, different app-profile PATH — the writers disagree on where the SoT value lives,
# so the bridge content depends on which writer ran.
  MAP = {
    "app.id"                             => "identity.app_id",
    "log.tag"                            => "network.WRONG_PATH",
  }
