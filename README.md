# gdpr

## Notice

An update tool for Wireshark pcap trace files to anonymize personally identifiable data in the traces. This will allow the storing of pcap files by anonymising any Personally Identifiable data.

![License](https://img.shields.io/badge/License-GPLv3-blue.svg)

This code will:-

* read all packets
* Locate any SIP packets in UDP or TCP packets
* Parse all SIP headers, anonymising the original MSISDNs
* store the updated SIP packets
* non-SIP packets are copied through un-altered

## Operation

Run the executable jar file with the name of the pcap file to be processes. A new parse pcap file will be created with the same packets and the MSISDNs replaced with new, anonymous MSISDNs.

Add the argument -v, or -vv for logging output.

## Future enhancements

In the future I may add in HTTP packets, SMPP packets or SIP-I packets.

## License

> This file is part of gdpr
> 
> Copyright (C) 2018 Jim Prince
>
>    This program is free software: you can redistribute it and/or modify
>    it under the terms of the GNU General Public License as published by
>    the Free Software Foundation, either version 3 of the License, or
>    (at your option) any later version.
>
>    This program is distributed in the hope that it will be useful,
>    but WITHOUT ANY WARRANTY; without even the implied warranty of
>    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
>    GNU General Public License for more details.
>
>    You should have received a copy of the GNU General Public License
>    along with this program.  If not, see <http://www.gnu.org/licenses/>.
>    See LICENSE for information.
