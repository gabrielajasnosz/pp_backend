// SPDX-License-Identifier: MIT
pragma solidity >=0.4.22 <0.9.0;
 
contract CertificateRegistry {

    address contractOwner;

    struct Recipient{ // recipier description
        string name;
        string surname;
    }
 
     struct Certificate {
        string checksum; // path to certificate
        uint256 timestamp;
        address issuer;
        Recipient recipient;
    }
 
    mapping(string => Certificate) certificates;
 
    mapping(address => bool) public trustedIssuers;

    constructor() {
        contractOwner = msg.sender;
        trustedIssuers[msg.sender] = true; // first admin address being used to add trusted issuers
    }
 
    modifier onlyTrustedIssuer() {
        require(trustedIssuers[msg.sender], "You are not a trusted issuer.");
        _;
    }
 
    //TODO 1: Walidatory (puste pola itd)
    function addCertificate(string memory _recipient_name, string memory _recipient_surname, string memory _checksum, address _issuer) public onlyTrustedIssuer{
        certificates[_checksum] = Certificate(_checksum, block.timestamp, _issuer, Recipient(_recipient_name, _recipient_surname));
    }

    function addTrustedIssuer(address _newIssuer) public onlyTrustedIssuer {
        trustedIssuers[_newIssuer] = true;
    }

    function removeTrustedIssuer(address _issuer) public onlyTrustedIssuer {
        if(_issuer != contractOwner){
            trustedIssuers[_issuer] = false;
        }
    }

    //TODO 2: getCertificate

    function getCertificate (string memory _checksum) public view onlyTrustedIssuer returns (Certificate memory result) {
        result = certificates[_checksum];
    }
}