const CertificateRegistry = artifacts.require("./CertificateRegistry.sol");

module.exports = function(deployer) {
  deployer.deploy(CertificateRegistry);
};
